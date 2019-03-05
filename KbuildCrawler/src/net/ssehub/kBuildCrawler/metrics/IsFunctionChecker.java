package net.ssehub.kBuildCrawler.metrics;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Properties;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.code_model.ast.Function;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.code_model.ast.ISyntaxElementVisitor;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.srcml.SrcMLExtractor;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * A helper class to check if a given file and line number lies within a function in the source file.
 * 
 * @author Adam
 */
public class IsFunctionChecker {

    private File sourceTree;
    
    private File resourceDir;
    
    public IsFunctionChecker(File sourceTree) {
        this.sourceTree = sourceTree;
        this.resourceDir = new File("kh/res");
    }
    
    // wrapped inside a class, so its accessible to the anonymous inner class below
    private static class ResultHolder {
        
        private boolean isInFunction;
        
        private String functionName;
        
        private boolean error;
        
    }
    
    private ResultHolder getFunctionImpl(File file, int line) {
        ResultHolder resultHolder = new ResultHolder();
        
        try {
            Properties props = new Properties();
            props.setProperty(DefaultSettings.SOURCE_TREE.getKey(), sourceTree.getAbsolutePath());
            props.setProperty(DefaultSettings.RESOURCE_DIR.getKey(), resourceDir.getAbsolutePath());
            props.setProperty(DefaultSettings.CODE_EXTRACTOR_FILES.getKey(), file.getPath());
            
            Configuration config = new Configuration(props);
            config.registerSetting(DefaultSettings.SOURCE_TREE);
            config.registerSetting(DefaultSettings.RESOURCE_DIR);
            config.registerSetting(DefaultSettings.CODE_EXTRACTOR_FILES);
            
            SrcMLExtractor extractor = new SrcMLExtractor();
            
            Method initMethod = extractor.getClass().getDeclaredMethod("init", Configuration.class);
            initMethod.setAccessible(true);
            initMethod.invoke(extractor, config);
            
            Method runOnFileMethod = extractor.getClass().getDeclaredMethod("runOnFile", File.class);
            runOnFileMethod.setAccessible(true);
            // srcML returns SourceFile<ISyntaxElement> elements
            @SuppressWarnings("unchecked")
            SourceFile<ISyntaxElement> result = (SourceFile<ISyntaxElement>) runOnFileMethod.invoke(extractor, file);
            
            resultHolder.isInFunction = false;
            resultHolder.functionName = null;
            for (ISyntaxElement element : result) {
                element.accept(new ISyntaxElementVisitor() {
                    
                    @Override
                    public void visitFunction(@NonNull Function function) {
                        
                        if (function.getLineStart() <= line && function.getLineEnd() >= line) {
                            resultHolder.isInFunction = true;
                            resultHolder.functionName = function.getName();
                        }
                        
                        // no recursive visitation of children needed
                    }
                    
                });
                
                if (resultHolder.isInFunction) {
                    break;
                }
            }
            
        } catch (ReflectiveOperationException | ClassCastException | SecurityException | SetUpException e) {
            Logger.get().logException("Couldn't extract file \"" + file.getPath() + "\", assuming that line is a function", e);
            resultHolder.error = true;
        }
        
        return resultHolder;
    }
    
    /**
     * Returns the name of the function that the given line in the given file is in.
     * 
     * @param file The file to check in.
     * @param line The line number to get the function name for.
     * 
     * @return The function name, or <code>null</code> if the given line is not inside a function (or could not be
     *      determined).
     */
    public String getFunctionName(File file, int line) {
        ResultHolder result = getFunctionImpl(file, line);
        return result.functionName;
    }
    
    public boolean isWithinFunction(File file, int line) {
        ResultHolder result = getFunctionImpl(file, line);
        return result.isInFunction || result.error;
    }
    
}
