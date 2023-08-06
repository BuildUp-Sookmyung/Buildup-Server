package buildup.server.record.service;

import buildup.server.activity.domain.Activity;
import buildup.server.activity.exception.ActivityErrorCode;
import buildup.server.activity.exception.ActivityException;
import buildup.server.activity.repository.ActivityRepository;
import buildup.server.category.CategoryRepository;
import buildup.server.category.CategoryService;
import buildup.server.member.domain.Member;
import buildup.server.member.exception.MemberErrorCode;
import buildup.server.member.exception.MemberException;
import buildup.server.member.repository.MemberRepository;
import buildup.server.member.service.MemberService;
import buildup.server.member.service.S3Service;
import buildup.server.record.dto.*;
import buildup.server.record.exception.RecordErrorCode;
import buildup.server.record.exception.RecordException;
import buildup.server.record.domain.Record;
import buildup.server.record.domain.RecordImg;
import buildup.server.record.repository.RecordImgRepository;
import buildup.server.record.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordService {
    private final ActivityRepository activityRepository;
    private final MemberRepository memberRepository;
    private final RecordRepository recordRepository;
    private final RecordImgRepository recordImgRepository;
    private final MemberService memberService;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final S3Service s3Service;

    @Transactional
    public Long createRecord(RecordSaveRequest requestDto, List<MultipartFile> multipartFiles) {

        if (multipartFiles == null || multipartFiles.isEmpty()) {
            throw new RecordException(RecordErrorCode.WRONG_INPUT_CONTENT);
        }
        Activity activity = activityRepository.findById(requestDto.getActivityId())
                .orElseThrow(() -> new ActivityException(ActivityErrorCode.ACTIVITY_NOT_FOUND));
        Record record = requestDto.toRecord(activity);
        //TODO:set을 updaterecord로 고치기
        record.setActivity(activity);
        recordRepository.save(record);

        for (MultipartFile multipartFile : multipartFiles) {
            String imgUrl = s3Service.uploadOneRecord(multipartFile);
            RecordImg recordImg = new RecordImg(imgUrl, record);
            recordImgRepository.save(recordImg);
        }

        return record.getId();
    }



    @Transactional(readOnly = true)
    public RecordResponse readOneRecord(Long recordId) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RecordException(RecordErrorCode.NOT_FOUND_RECORD));

        List<String> imgUrls = recordImgRepository.findAllByRecord(record)
                .stream()
                .map(RecordImg::getStoreUrl)
                .collect(Collectors.toList());

        return new RecordResponse(recordId, record, imgUrls);
    }

    @Transactional(readOnly = true)
    public List<RecordListResponse> readAllRecordByActivity(Long activityId) {

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ActivityException(ActivityErrorCode.ACTIVITY_NOT_FOUND));
        return RecordListResponse.toDtoList(recordRepository.findAllByActivity(activity));

    }
    @Transactional
    public void updateRecords(RecordUpdateRequest requestDto) {
        Record record = recordRepository.findById(requestDto.getId())
                .orElseThrow(() -> new RecordException(RecordErrorCode.NOT_FOUND_RECORD));
        record.updateRecord(requestDto.getRecordTitle(), requestDto.getExperienceName(), requestDto.getConceptName(),
                requestDto.getResultName(), requestDto.getContent(), requestDto.getDate(), requestDto.getUrlName());
    }

    @Transactional
    public void updateRecordImage(RecordImageUpdateRequest requestDto, List<MultipartFile> multipartFiles){

        Record record = recordRepository.findById(requestDto.getRecordid())
                .orElseThrow(() -> new RecordException(RecordErrorCode.NOT_FOUND_RECORD));
        List<RecordImg> recordImagesByRecordId = recordImgRepository.findByRecordId(requestDto.getRecordid());

        int existingImagesCount = recordImagesByRecordId.size();
        int newImagesCount = multipartFiles.size();

        List<String> imgUrls = s3Service.uploadRecord(multipartFiles);

        for(int i=0; i<newImagesCount; i++) {
            if(i < existingImagesCount) {
                String old_url = recordImagesByRecordId.get(i).getStoreUrl();
                if(old_url != null){
                    s3Service.deleteOneRecord(old_url);
                }
                recordImagesByRecordId.get(i).setStoreUrl(imgUrls.get(i));
            } else {
                RecordImg newRecordImg = new RecordImg(imgUrls.get(i), record);
                recordImgRepository.save(newRecordImg);
            }
        }

        for(int i=newImagesCount; i<existingImagesCount; i++) {
            s3Service.deleteOneRecord(recordImagesByRecordId.get(i).getStoreUrl());
            recordImgRepository.delete(recordImagesByRecordId.get(i));
        }
    }

    @Transactional
    public void deleteRecords(List<String> idList){
        log.info("try to delete records id: {}", idList.toString());
        for(String id : idList){
            deleteRecord(Long.parseLong(id));
        }
    }

    private RecordImgRequest updateOneImage(MultipartFile multipartFile){
        return RecordImgRequest.builder()
                .storeUrl(s3Service.uploadOneRecord(multipartFile))
                .build();
    }

    private void putRequestParser(List<RecordImg> recordImgList, List<RecordImgRequest> recordImgRequestList){
        for (RecordImgRequest recordImgRequest : recordImgRequestList){
            RecordImg recordImg = RecordImg.builder()
                    .storeUrl(recordImgRequest.getStoreUrl())
                    .build();
            recordImgList.add(recordImg);
        }
    }
    private void deleteRecord(Long id) {
        Record record = recordRepository.findById(id)
                .orElseThrow(() -> new RecordException(RecordErrorCode.NOT_FOUND_RECORD));
        List<RecordImg> recordImages = recordImgRepository.findAllByRecord(record);
        recordImgRepository.deleteAll(recordImages);
        recordRepository.delete(record);
    }



}
