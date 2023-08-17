package buildup.server.record.dto;

import buildup.server.activity.domain.Activity;
import buildup.server.record.domain.Record;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class RecordSaveRequest {

    @NotNull
    private Long activityId;
    @NotBlank
    private String recordTitle;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String experienceName;

    private String conceptName;

    @NotBlank
    private String resultName;

    private String content;

    private String urlName;

    public Record toRecord(Activity activity) {
        return Record.builder()
                .title(recordTitle)
                .date(date)
                .experience(experienceName)
                .concept(conceptName)
                .result(resultName)
                .content(content)
                .url(urlName)
                .activity(activity)
                .build();
    }


}
