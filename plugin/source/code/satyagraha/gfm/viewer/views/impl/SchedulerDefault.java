package code.satyagraha.gfm.viewer.views.impl;

import static org.apache.commons.io.FileUtils.iterateFiles;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;

import code.satyagraha.gfm.di.Component;
import code.satyagraha.gfm.support.api.Transformer;
import code.satyagraha.gfm.viewer.plugin.Activator;
import code.satyagraha.gfm.viewer.views.api.Scheduler;

@Component
public class SchedulerDefault implements Scheduler {

    private final Transformer transformer;
    private final IOFileFilter markdownFileFilter;
  
    public SchedulerDefault(Transformer transformer) {
        this.transformer = transformer;
        
        markdownFileFilter = new IOFileFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return accept(new File(dir, name));
            }

            @Override
            public boolean accept(File file) {
                return SchedulerDefault.this.transformer.isMarkdownFile(file);
            }
        };
    }
    
    @Override
    public void scheduleTransformation(final File mdFile, final Callback<File> onDone) {
        final String jobName = "Transforming: " + mdFile.getName();
        final File htFile = transformer.createHtmlFile(mdFile);
        Job job = new Job(jobName) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    transformer.transformMarkdownFile(mdFile, htFile);
                } catch (IOException e) {
                    status = new Status(Status.ERROR, Activator.PLUGIN_ID, jobName, e);
                }
                return status;
            }
        };
        job.setUser(false);
        job.setSystem(false);
        job.setPriority(Job.SHORT);
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
//                in principle, the following line should be enabled, but it appears to force project rebuild
//                refreshFile(htFile);
                if (event.getResult().isOK()) {
                    if (onDone != null) {
                        Display.getDefault().asyncExec(new Runnable() {
                            
                            @Override
                            public void run() {
                                onDone.onComplete(htFile);
                            }
                        });
                    }
                } else {
                    // normal reporting has occurred
                }
            }

        });
        job.schedule();
    }

    @Override
    public void generateIFile(IFile iFile) {
        Activator.debug("iFile: " + iFile);
        generateFile(iFile.getRawLocation().toFile());
    }

    @Override
    public void generateIFolder(IFolder iFolder) {
        Activator.debug("iFolder: " + iFolder);
        File folder = iFolder.getRawLocation().toFile();
        for (Iterator<File> files = iterateFiles(folder, markdownFileFilter, TrueFileFilter.INSTANCE); files.hasNext();) {
            File file = files.next();
            generateFile(file);
        }
    }

    private void generateFile(File mdFile) {
        Activator.debug("mdFile: " + mdFile);
        scheduleTransformation(mdFile, null);
    }
    
}
