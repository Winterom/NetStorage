package message;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class Command implements Serializable {
    @Getter@Setter
    CommandType type;
}
