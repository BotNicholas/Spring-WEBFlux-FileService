package org.example.videoviewer.exceptions;

public class Constants {
    private Constants() {}

    public static final String METADATA_EXCEPTION_ERROR_MESSAGE = "Metadata is not valid";

    public static class Templates {
        public static final String FILE_ALREADY_EXISTS_ERROR_TEMPLATE = "File with name '%s' already exists";
        public static final String FILE_NOT_FOUND_ERROR_TEMPLATE = "'%s' does not exists";
        public static final String METADATA_EXCEPTION_TEMPLATE = "Metadata is not valid: %s";
        public static final String AUTHENTICATION_EXCEPTION_TEMPLATE = "Authentication failed: %s";
    }
}
