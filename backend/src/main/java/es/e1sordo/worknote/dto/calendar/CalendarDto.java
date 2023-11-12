package es.e1sordo.worknote.dto.calendar;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

@Data
public class CalendarDto {

    @JacksonXmlProperty(localName = "year")
    private int year; // the year for which the calendar was generated

    @JacksonXmlProperty(localName = "lang")
    private String lang; // two-letter code of the language in which the names of the holidays are presented

    @JacksonXmlProperty(localName = "date")
    private String date; // date of generation of the xml calendar in the format YYYY.MM.DD

    @JacksonXmlProperty(localName = "country")
    private String country; // two-letter country code

    @JacksonXmlElementWrapper(localName = "holidays")
    @JacksonXmlProperty(localName = "holiday")
    private List<HolidayDto> holidays;

    @JacksonXmlElementWrapper(localName = "days")
    @JacksonXmlProperty(localName = "day")
    private List<DayDto> days;
}
