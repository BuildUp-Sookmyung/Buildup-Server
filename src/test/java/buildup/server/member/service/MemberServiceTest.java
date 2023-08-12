package buildup.server.member.service;

import buildup.server.auth.domain.AuthTokenProvider;
import buildup.server.auth.domain.MemberRefreshToken;
import buildup.server.auth.dto.TokenDto;
import buildup.server.auth.service.AuthService;
import buildup.server.common.DummyObject;
import buildup.server.member.domain.Member;
import buildup.server.member.domain.Profile;
import buildup.server.member.dto.LocalJoinRequest;
import buildup.server.member.dto.ProfileSaveRequest;
import buildup.server.member.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest extends DummyObject {

    @InjectMocks
    private MemberService memberService;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ProfileService profileService;
    @Mock
    private AuthService authService;
    @Spy
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    public void 일반회원가입_test() throws Exception {
        // given
        AuthTokenProvider tokenProvider = new AuthTokenProvider("jdakslfjwfsndjnvkafwkfsksldnfldkslfnkenfwnaefnnwenfjkajgknksjnfgjwen");

        ArrayList<String> interests = new ArrayList<>();
        interests.add("연구/개발");
        interests.add("디자인");
        ProfileSaveRequest profileSaveRequest = new ProfileSaveRequest("jojo",
                "username@naver.com",
                "Sookmyung Women's University",
                "Computer Science", "4", "N", interests);
        LocalJoinRequest request = new LocalJoinRequest(
                "username",
                passwordEncoder.encode("password4321"),
                profileSaveRequest,
                "Y"
        );
        Member member = newMember(request);
        Profile profileEntity = request.getProfile().toProfile(member);

        Mockito.when(memberRepository.findAllByEmailAndUsername(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(new ArrayList<>());
        Mockito.when(memberRepository.save(ArgumentMatchers.any())).thenReturn(member);
        Mockito.when(profileService.saveProfile(profileSaveRequest, member)).thenReturn(profileEntity.getId());
        Mockito.when(authService.createAuth(ArgumentMatchers.any())).thenReturn(tokenProvider.createAuthToken(member.getUsername(), new Date(new Date().getTime()+1800000)));
        Mockito.when(authService.setRefreshToken(ArgumentMatchers.any())).thenReturn(new MemberRefreshToken(member.getUsername(), "refreshToken"));

        Mockito.when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));


        // when
        T
        okenDto tokenDto = memberService.join(request);
        Optional<Member> memberOptional = memberRepository.findById(member.getId());

        // then
        Assertions.assertThat(member.getId()).isEqualTo(memberOptional.get().getId());
        Assertions.assertThat(!tokenDto.getAccessToken().isEmpty()).isTrue();
    }
}