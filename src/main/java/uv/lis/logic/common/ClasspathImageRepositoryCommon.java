package uv.lis.logic.common;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jasperreports.repo.InputStreamResource;
import net.sf.jasperreports.repo.RepositoryService;
import net.sf.jasperreports.repo.Resource;

public final class ClasspathImageRepositoryCommon implements RepositoryService {

    private static final Logger LOGGER = Logger.getLogger(ClasspathImageRepositoryCommon.class.getName());

    private static final String IMAGES_CLASSPATH = "/uv/lis/GUI/view/images/";
    private static final char WINDOWS_SEPARATOR = '\\';
    private static final char UNIX_SEPARATOR = '/';
    private static final int FILE_NAME_START_OFFSET = 1;

    @Override
    public <Key extends Resource> Key getResource(String uri, Class<Key> resourceType) {
        Key resource = null;

        if (uri != null && InputStreamResource.class.equals(resourceType)) {
            resource = resourceType.cast(buildImageResource(uri));
        }
        return resource;
    }

    @Override
    public Resource getResource(String uri) {
        Resource resource = getResource(uri, InputStreamResource.class);
        return resource;
    }

    @Override
    public void saveResource(String uri, Resource resource) {
        LOGGER.log(Level.WARNING, "Operación de guardado no soportada para {0}", uri);
    }

    private InputStreamResource buildImageResource(String uri) {
        InputStreamResource imageResource = null;
        String imageName = extractFileName(uri);
        InputStream imageStream = getClass().getResourceAsStream(IMAGES_CLASSPATH + imageName);

        if (imageStream == null) {
            LOGGER.log(Level.WARNING, "No se encontró la imagen en el classpath: {0}", imageName);
        } else {
            imageResource = new InputStreamResource();
            imageResource.setInputStream(imageStream);
        }
        return imageResource;
    }

    private String extractFileName(String uri) {
        int lastWindowsSeparator = uri.lastIndexOf(WINDOWS_SEPARATOR);
        int lastUnixSeparator = uri.lastIndexOf(UNIX_SEPARATOR);
        int lastSeparator = Math.max(lastWindowsSeparator, lastUnixSeparator);
        String fileName = uri.substring(lastSeparator + FILE_NAME_START_OFFSET);
        return fileName;
    }
}