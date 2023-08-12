package buildup.server.activity.service;

import buildup.server.activity.domain.Activity;
import buildup.server.activity.dto.ActivitySaveRequest;
import buildup.server.activity.dto.ActivityUpdateRequest;
import buildup.server.activity.exception.ActivityErrorCode;
import buildup.server.activity.exception.ActivityException;
import buildup.server.activity.repository.ActivityRepository;
import buildup.server.category.Category;
import buildup.server.category.CategoryRepository;
import buildup.server.category.CategoryService;
import buildup.server.category.exception.CategoryErrorCode;
import buildup.server.category.exception.CategoryException;
import buildup.server.member.domain.Member;
import buildup.server.member.service.MemberService;
import buildup.server.record.domain.Record;
import buildup.server.record.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityFormService {

    private final MemberService memberService;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final ActivityRepository activityRepository;
    private final RecordRepository recordRepository;

    public Activity saveActivity(ActivitySaveRequest requestDto) {

        Member member = memberService.findCurrentMember();
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        categoryService.checkCategoryAuthForRead(member, category);

        Activity activity = requestDto.toActivity(member, category);
        return activityRepository.save(activity);
    }

    public void updateActivity(ActivityUpdateRequest requestDto) {
        Activity activity = activityRepository.findById(requestDto.getId())
                .orElseThrow(() -> new ActivityException(ActivityErrorCode.ACTIVITY_NOT_FOUND));
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        activity.updateActivity(category, requestDto.getActivityName(), requestDto.getHostName(), requestDto.getRoleName(),
                requestDto.getStartDate(), requestDto.getEndDate(),requestDto.getUrlName());
    }

    public void deleteActivity(Long id) {
        Activity activity= activityRepository.findById(id)
                .orElseThrow(() -> new ActivityException(ActivityErrorCode.ACTIVITY_NOT_FOUND));
        checkActivityAuth(activity, memberService.findCurrentMember());
        List<Record> childRecords = recordRepository.findAllByActivity(activity);
        recordRepository.deleteAll(childRecords);
        activityRepository.delete(activity);
    }

    private void checkActivityAuth(Activity activity, Member member) {
        if (! activity.getMember().equals(member))
            throw new ActivityException(ActivityErrorCode.ACTIVITY_NO_AUTH);
    }
}
