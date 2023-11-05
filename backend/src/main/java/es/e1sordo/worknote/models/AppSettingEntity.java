package es.e1sordo.worknote.models;

import es.e1sordo.worknote.enums.AppSettingKeys;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = AppSettingEntity.TABLE_NAME)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AppSettingEntity {

    static final String TABLE_NAME = "settings";

    @Id
    private AppSettingKeys key;
    private String value;
}
