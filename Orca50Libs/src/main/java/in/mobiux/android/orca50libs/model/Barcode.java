package in.mobiux.android.orca50libs.model;

import in.mobiux.android.commonlibs.utils.AppUtils;

/**
 * Created by SUJEET KUMAR on 21-May-21.
 */
public class Barcode extends BaseModel {

    private String name;


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
