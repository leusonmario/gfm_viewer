package code.satyagraha.gfm.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import code.satyagraha.gfm.viewer.plugin.Activator;
import code.satyagraha.gfm.viewer.views.MarkdownView;

public class Reload extends AbstractHandler {

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        Activator.debug("");
        // locate the view and perform action
        MarkdownView.getInstance().reload();
        return null;
    }

}
