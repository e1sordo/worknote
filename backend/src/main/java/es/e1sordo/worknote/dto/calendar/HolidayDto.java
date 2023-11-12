package es.e1sordo.worknote.dto.calendar;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class HolidayDto {

    @JacksonXmlProperty(localName = "id")
    private int id;

    @JacksonXmlProperty(localName = "title")
    private String title;
}
