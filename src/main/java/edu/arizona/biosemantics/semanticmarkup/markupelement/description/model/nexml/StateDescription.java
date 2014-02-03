package edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.nexml;

import edu.arizona.biosemantics.semanticmarkup.markupelement.description.model.Description;

public class StateDescription extends Description {

    @Override
    /**
     * It is necessary to override this from Description for MOXy to pick up the correct bindings configuration
     * @return text
     */
    public String getText() {
        return super.getText();
    }

    @Override
    /**
     * It is necessary to override this from Description for MOXy to pick up the correct bindings configuration
     * @param text
     */
    public void setText(String text) {
        super.setText(text);
    } 

}
