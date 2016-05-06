package com.springml.sftp.client;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public class FileNameFilter implements FilenameFilter {
    private String pattern;

    public FileNameFilter(String pattern) {
        this.pattern = pattern.replaceAll("\\*", ".*");
    }

    public boolean accept(File dir, String name) {
        return Pattern.matches(pattern, name);
    }

}
