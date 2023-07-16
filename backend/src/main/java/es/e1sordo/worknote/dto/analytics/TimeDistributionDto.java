package es.e1sordo.worknote.dto.analytics;

import es.e1sordo.worknote.models.JiraTaskType;

import java.time.LocalDate;
import java.util.Map;

public record TimeDistributionDto(Map<JiraTaskType, Integer> byTypes,
                                  LocalDate periodEnd) {
}

/*

{
    "data": [
        {
            "byTypes": {
                "DEVELOPMENT": 54,
                "MAINTANENCE": 20,
            }
            "periodLabel": "2022-12-01"
        }
    ]

 */
