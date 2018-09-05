package net.ssehub.kBuildCrawler.metrics;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.code_model.CodeElement;
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
    
    public boolean isWithinFunction(File file, int line) {
        Properties props = new Properties();
        props.setProperty("resource_dir", resourceDir.getAbsolutePath());

        // isInFunction is wrapped inside a class, so its accessible to the anonymous inner class below
        class Holder {
            boolean isInFunction; 
        }
        Holder holder = new Holder();
        
        try {
            Configuration config = new Configuration(new Properties());
            
            config.registerSetting(DefaultSettings.SOURCE_TREE);
            config.setValue(DefaultSettings.SOURCE_TREE, sourceTree);
            
            config.registerSetting(DefaultSettings.RESOURCE_DIR);
            config.setValue(DefaultSettings.RESOURCE_DIR, resourceDir);
            
            config.registerSetting(DefaultSettings.CODE_EXTRACTOR_FILES);
            config.setValue(DefaultSettings.CODE_EXTRACTOR_FILES, Arrays.asList(file.getPath()));
            
            SrcMLExtractor extractor = new SrcMLExtractor();
            
            Method initMethod = extractor.getClass().getMethod("init", Configuration.class);
            initMethod.setAccessible(true);
            initMethod.invoke(extractor, config);
            
            Method runOnFileMethod = extractor.getClass().getMethod("runOnFile", File.class);
            runOnFileMethod.setAccessible(true);
            SourceFile result = (SourceFile) runOnFileMethod.invoke(extractor, file);
            
            holder.isInFunction = false;
            for (CodeElement element : result) {
                ((ISyntaxElement) element).accept(new ISyntaxElementVisitor() {
                    
                    @Override
                    public void visitFunction(@NonNull Function function) {
                        
                        if (function.getLineStart() <= line && function.getLineEnd() >= line) {
                            holder.isInFunction = true;
                        }
                        
                        // no recursive visitation of children needed
                    }
                    
                });
                
                if (holder.isInFunction) {
                    break;
                }
            }
            
        } catch (ReflectiveOperationException | ClassCastException | SecurityException | SetUpException e) {
            Logger.get().logException("Couldn't extract file \"" + file.getPath() + "\", assuming that line is a function", e);
            holder.isInFunction = true;
        }
        
        return holder.isInFunction;
    }
    
}
