package es.e1sordo.worknote.dto.calendar;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class DayDto {

    // Saturday and Sunday are considered days off if there are no day tags with the attribute t=2 and t=3 for this day

    @JacksonXmlProperty(localName = "d")
    private String day; // day (MM.DD format)

    @JacksonXmlProperty(localName = "t")
    private int type; // day type: 1 - weekend, 2 - working and shortened (can be used for any day of the week), 3 - working day (Saturday/Sunday)

    @JacksonXmlProperty(localName = "h")
    private Integer holiday; // holiday number (link to the id attribute of the holiday tag)

    @JacksonXmlProperty(localName = "f")
    private String from; // date from which the day off was moved
}
