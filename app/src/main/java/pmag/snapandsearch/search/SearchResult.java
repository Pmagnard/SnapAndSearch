package pmag.snapandsearch.search;

import java.io.File;

/**
 * Created by FR067458 on 25/01/2016.
 */
public class SearchResult {
    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = image;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private File image;
    private String imageName;
    private String comment;
    private String url;

}
