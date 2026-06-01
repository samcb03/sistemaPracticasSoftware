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
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';

    //TODO estudiar clase
    @Override
    public <K extends Resource> K getResource(String uri, Class<K> resourceType) {
        K resource = null;

        if (uri != null && InputStreamResource.class.equals(resourceType)) {
            resource = resourceType.cast(buildImageResource(uri));
        }
        return resource;
    }

    @Override
    public Resource getResource(String uri) {
        return null;
    }

    @Override
    public void saveResource(String uri, Resource resource) {
        LOGGER.log(Level.FINE, "Operación de guardado no soportada para {0}", uri);
    }

    private InputStreamResource buildImageResource(String uri) {
        InputStreamResource imageResource = null;
        String imageName = extractFileName(uri);
        InputStream imageStream = getClass().getResourceAsStream(IMAGES_CLASSPATH + imageName);

        if (imageStream != null) {
            imageResource = new InputStreamResource();
            imageResource.setInputStream(imageStream);
        }
        return imageResource;
    }

    private String extractFileName(String uri) {
        int lastUnixSeparator = uri.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsSeparator = uri.lastIndexOf(WINDOWS_SEPARATOR);
        int lastSeparator = Math.max(lastUnixSeparator, lastWindowsSeparator);
        return uri.substring(lastSeparator + 1);
    }
}