package ss.spellid.components;

import org.ladysnake.cca.api.v3.component.Component;

public interface FlawComponent extends Component {
    void setInt(String key, int value);
    int getInt(String key, int defaultValue);
    void setLong(String key, long value);
    long getLong(String key, long defaultValue);
}