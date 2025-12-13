package tla.domain;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import tools.jackson.databind.ObjectMapper;

import tla.domain.dto.AnnotationDto;
import tla.domain.dto.CorpusObjectDto;
import tla.domain.dto.LemmaDto;
import tla.domain.dto.TextDto;
import tla.domain.dto.ThsEntryDto;
import tla.domain.dto.meta.AbstractDto;

public class Util {

    static final String RESOURCE_PATH = "src/test/resources/sample/";

    public static final Map<Class<? extends AbstractDto>, String> SAMPLE_DIR_PATHS = Map.of(
        LemmaDto.class, "lemma",
        TextDto.class, "text",
        ThsEntryDto.class, "ths",
        AnnotationDto.class, "annotation",
        CorpusObjectDto.class, "object"
    );

    private static ObjectMapper mapper = new ObjectMapper();

    public static String getSampleFileFullPath(String path, String filename) {
        return String.format(
            RESOURCE_PATH + "%s/%s",
            path,
            filename
        );
    }

    public static String loadFromFileAsString(String path, String filename) throws Exception {
        return Files.readString(
            new File(
                getSampleFileFullPath(path, filename)
            ).toPath(),
            StandardCharsets.UTF_8
        );
    }

    public static AbstractDto loadFromFile(String path, String filename) throws Exception {
        return mapper.readValue(
            new File(
                getSampleFileFullPath(path, filename)
            ),
            AbstractDto.class
        );
    }
}