package semanticMarkup.io.output;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class BooleanAdapter extends XmlAdapter<Boolean, Boolean> {

    @Override
    public Boolean unmarshal(Boolean value) throws Exception {
        return Boolean.TRUE.equals(value);
    }

    @Override
    public Boolean marshal(Boolean value) throws Exception {
        if(value) {
            return value;
        }
        return null;
    }

}