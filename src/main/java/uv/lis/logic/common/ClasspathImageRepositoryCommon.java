package uv.lis.logic.common;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jasperreports.repo.InputStreamResource;
import net.sf.jasperreports.repo.RepositoryService;
import net.sf.jasperreports.repo.Resource;

/**
 * Resolves images embedded in JasperReports templates by locating them on the
 * classpath instead of the file system. Register an instance of this class as
 * a RepositoryService extension on a SimpleJasperReportsContext so that
 * JasperReports delegates every image lookup to it during report generation.
 */
public final class ClasspathImageRepositoryCommon implements RepositoryService {

    private static final Logger LOGGER = Logger.getLogger(ClasspathImageRepositoryCommon.class.getName());
    private static final String IMAGES_CLASSPATH = "/uv/lis/GUI/view/images/";
    private static final char WINDOWS_SEPARATOR = '\\';
    private static final char UNIX_SEPARATOR = '/';
    private static final int FILE_NAME_START_OFFSET = 1;

    /**
     * Returns the image at the given URI as an InputStreamResource loaded from
     * the classpath, or null if the URI is null or the requested type is not
     * InputStreamResource.
     *
     * @param UniformResourceIdentifier the image URI as it appears in the report template
     * @param resourceType the resource type requested by JasperReports
     * @return the image wrapped in an InputStreamResource, or null if the
     * resource cannot be resolved
     */
    @Override
    public <Key extends Resource> Key getResource(String UniformResourceIdentifier, Class<Key> resourceType) {
        Key resource = null;

        if (UniformResourceIdentifier != null && InputStreamResource.class.equals(resourceType)) {
            resource = resourceType.cast(buildImageResource(UniformResourceIdentifier));
        }
        return resource;
    }

    /**
     * Returns the image at the given URI as a Resource loaded from the
     * classpath, or null if the image is not found.
     *
     * @param UniformResourceIdentifier the image URI as it appears in the report template
     * @return the image wrapped in an InputStreamResource, or null if the
     *         resource cannot be resolved
     */
    @Override
    public Resource getResource(String UniformResourceIdentifier) {
        Resource resource = getResource(UniformResourceIdentifier, InputStreamResource.class);
        return resource;
    }

    /**
     * Not supported. This repository is read-only; calling this method logs a
     * warning and performs no action.
     *
     * @param UniformResourceIdentifier the URI where the resource would be saved
     * @param resource the resource to save
     */
    @Override
    public void saveResource(String UniformResourceIdentifier, Resource resource) {
        LOGGER.log(Level.WARNING, "Operación de guardado no soportada para {0}", UniformResourceIdentifier);
    }

    private InputStreamResource buildImageResource(String UniformResourceIdentifier) {
        InputStreamResource imageResource = null;
        String imageName = extractFileName(UniformResourceIdentifier);
        InputStream imageStream = getClass().getResourceAsStream(IMAGES_CLASSPATH + imageName);

        if (imageStream == null) {
            LOGGER.log(Level.WARNING, "No se encontró la imagen en el classpath: {0}", imageName);
        } else {
            imageResource = new InputStreamResource();
            imageResource.setInputStream(imageStream);
        }
        return imageResource;
    }

    private String extractFileName(String UniformResourceIdentifier) {
        int lastWindowsSeparator = UniformResourceIdentifier.lastIndexOf(WINDOWS_SEPARATOR);
        int lastUnixSeparator = UniformResourceIdentifier.lastIndexOf(UNIX_SEPARATOR);
        int lastSeparator = Math.max(lastWindowsSeparator, lastUnixSeparator);
        String fileName = UniformResourceIdentifier.substring(lastSeparator + FILE_NAME_START_OFFSET);
        return fileName;
    }
}