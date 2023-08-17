package buildup.server.activity.service;

import buildup.server.activity.domain.Activity;
import buildup.server.activity.dto.ActivitySaveRequest;
import buildup.server.activity.repository.ActivityRepository;
import buildup.server.category.Category;
import buildup.server.category.CategoryRepository;
import buildup.server.category.CategoryService;
import buildup.server.common.DummyObject;
import buildup.server.member.domain.Member;
import buildup.server.member.domain.Profile;
import buildup.server.member.dto.LocalJoinRequest;
import buildup.server.member.dto.ProfileSaveRequest;
import buildup.server.member.service.MemberService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest extends DummyObject {

    @InjectMocks
    private ActivityService activityService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private MemberService memberService;
    @Mock
    private CategoryService categoryService;
    @Spy
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    public void 활동데이터_test() {
        ArrayList<String> interests = new ArrayList<>();
        interests.add("연구/개발");
        interests.add("디자인");
        LocalJoinRequest request = new LocalJoinRequest(
                "username",
                passwordEncoder.encode("password4321"),
                new ProfileSaveRequest("jojo",
                        "username@naver.com",
                        "Sookmyung Women's Universitiy",
                        "Computer Science", "4", "N", interests),
                "Y"
        );
        Member member = newMember(request);
        Profile profileEntity = request.getProfile().toProfile(member);
        profileEntity.setMember(member);

        Category category = new Category("스터디/동아리", 1L, member);
        ActivitySaveRequest dto = ActivitySaveRequest
                .builder()
                .activityName("JPA 스터디")
                .categoryId(1L)
                .roleName("member")
                .startDate(LocalDate.of(2023,8,1))
                .endDate(LocalDate.of(2023,8,31))
                .build();
        Activity activityToSave = dto.toActivity(member, category);

        Mockito.when(memberService.findCurrentMember()).thenReturn(member);
        Mockito.when(categoryRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(category));
        Mockito.when(activityRepository.save(ArgumentMatchers.any())).thenReturn(activityToSave);

        Activity activity = activityService.createActivity(dto);

        Optional<Activity> activityById = activityRepository.findById(member.getId());

        Assertions.assertThat(activity.getId()).isEqualTo(activityById.get().getId());
    }
}