package pmag.snapandsearch.test;

import java.io.File;

import pmag.snapandsearch.search.SearchImageHelper;

public class MainTestClass {
    /**
     * @param args
     */
    public static void main(String[] args) {
        SearchImageHelper searchHelper = new SearchImageHelper(false, "");

        // searchHelper.searchByImage(new
        // File("/home/pascal/dev/android.workspace/GoogleImageSearch/data/test.jpg"));
        System.out.println(searchHelper.searchBingByImage(new File(args[0])));
    }
}
