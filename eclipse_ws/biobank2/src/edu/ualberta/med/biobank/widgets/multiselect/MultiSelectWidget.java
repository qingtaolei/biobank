package edu.ualberta.med.biobank.widgets.multiselect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.widgets.BiobankWidget;
import edu.ualberta.med.biobank.widgets.listeners.TreeViewerDragListener;
import edu.ualberta.med.biobank.widgets.listeners.TreeViewerDropListener;

public class MultiSelectWidget extends BiobankWidget {

    private TreeViewer selTree;

    private TreeViewer availTree;

    private Button moveRightButton;

    private Button moveLeftButton;

    private Button moveUpButton;

    private Button moveDownButton;

    private MultiSelectNode selTreeRootNode = new MultiSelectNode(null, 0,
        "selRoot");

    private MultiSelectNode availTreeRootNode = new MultiSelectNode(null, 0,
        "availRoot");

    private int minHeight;

    public MultiSelectWidget(Composite parent, int style, String leftLabel,
        String rightLabel, int minHeight) {
        super(parent, style);

        this.minHeight = minHeight;

        setLayout(new GridLayout(4, false));
        setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        availTree = createLabelledTree(this, rightLabel);
        availTree.setInput(availTreeRootNode);
        availTree.setComparator(new ViewerComparator());

        Composite moveComposite = new Composite(this, SWT.NONE);
        moveComposite.setLayout(new GridLayout(1, false));
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.CENTER;
        gd.grabExcessVerticalSpace = true;
        moveComposite.setLayoutData(gd);
        moveRightButton = new Button(moveComposite, SWT.PUSH);
        moveRightButton.setImage(BioBankPlugin.getDefault().getImageRegistry()
            .get(BioBankPlugin.IMG_ARROW_RIGHT));
        moveRightButton.setToolTipText("Move to selected");
        moveLeftButton = new Button(moveComposite, SWT.PUSH);
        moveLeftButton.setImage(BioBankPlugin.getDefault().getImageRegistry()
            .get(BioBankPlugin.IMG_ARROW_LEFT));
        moveLeftButton.setToolTipText("Remove from selected");

        selTree = createLabelledTree(this, leftLabel);
        selTree.setInput(selTreeRootNode);
        selTree.setComparator(new ViewerComparator());

        Composite arrangeComposite = new Composite(this, SWT.NONE);
        arrangeComposite.setLayout(new GridLayout(1, false));
        gd = new GridData();
        gd.horizontalAlignment = SWT.CENTER;
        gd.grabExcessVerticalSpace = true;
        arrangeComposite.setLayoutData(gd);
        moveUpButton = new Button(arrangeComposite, SWT.PUSH);
        moveUpButton.setImage(BioBankPlugin.getDefault().getImageRegistry()
            .get(BioBankPlugin.IMG_ARROW_UP));
        moveUpButton.setToolTipText("Move up");
        moveDownButton = new Button(arrangeComposite, SWT.PUSH);
        moveDownButton.setImage(BioBankPlugin.getDefault().getImageRegistry()
            .get(BioBankPlugin.IMG_ARROW_DOWN));
        moveDownButton.setToolTipText("Move down");

        dragAndDropSupport(availTree, selTree);
        dragAndDropSupport(selTree, availTree);

        moveRightButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveTreeViewerSelection(availTree, selTree);
            }
        });

        moveLeftButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveTreeViewerSelection(selTree, availTree);
            }
        });
    }

    private void moveTreeViewerSelection(TreeViewer srcTree, TreeViewer destTree) {
        MultiSelectNode srcRootNode = (MultiSelectNode) srcTree.getInput();
        MultiSelectNode destRootNode = (MultiSelectNode) destTree.getInput();
        List<?> fromSelection = ((IStructuredSelection) srcTree.getSelection())
            .toList();

        for (Object obj : fromSelection) {
            MultiSelectNode node = (MultiSelectNode) obj;
            destRootNode.addChild(node);
            srcRootNode.removeChild(node);
            destTree.reveal(node);
            srcTree.refresh();
        }

    }

    private TreeViewer createLabelledTree(Composite parent, String label) {
        Composite selComposite = new Composite(parent, SWT.NONE);
        selComposite.setLayout(new GridLayout(1, true));
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        selComposite.setLayoutData(gd);

        Label l = new Label(selComposite, SWT.NONE);
        l.setText(label);
        l.setFont(new Font(null, "sans-serif", 8, SWT.BOLD));
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 2;
        gd.horizontalAlignment = SWT.CENTER;
        l.setLayoutData(gd);

        TreeViewer tv = new TreeViewer(selComposite);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = minHeight;
        gd.widthHint = 180;
        tv.getTree().setLayoutData(gd);

        tv.setLabelProvider(new MultiSelectNodeLabelProvider());
        tv.setContentProvider(new MultiSelectNodeContentProvider());

        return tv;
    }

    private void dragAndDropSupport(TreeViewer fromList, TreeViewer toList) {
        new TreeViewerDragListener(fromList);
        new TreeViewerDropListener(toList, this);
    }

    public void addSelections(LinkedHashMap<Integer, String> available,
        List<Integer> selected) {
        for (Integer key : available.keySet()) {
            if (selected.contains(key)) {
                selTreeRootNode.addChild(new MultiSelectNode(selTreeRootNode,
                    key, available.get(key)));
            } else {
                availTreeRootNode.addChild(new MultiSelectNode(
                    availTreeRootNode, key, available.get(key)));
            }
        }
    }

    /**
     * same as addSelections but remove previously set elements
     */
    public void setSelections(LinkedHashMap<Integer, String> available,
        List<Integer> selected) {
        selTreeRootNode.clear();
        availTreeRootNode.clear();
        addSelections(available, selected);
        selTreeRootNode.reset();
        availTreeRootNode.reset();
    }

    /**
     * Return the selected items in the order specified by user.
     * 
     */
    public List<Integer> getSelected() {
        List<Integer> result = new ArrayList<Integer>();
        for (MultiSelectNode node : selTreeRootNode.getChildren()) {
            result.add(new Integer(node.getId()));
        }
        return result;
    }

    public List<Integer> getAddedToSelection() {
        List<Integer> result = new ArrayList<Integer>();
        for (MultiSelectNode node : selTreeRootNode.getAddedChildren()) {
            result.add(new Integer(node.getId()));
        }
        return result;
    }

    public List<Integer> getRemovedToSelection() {
        List<Integer> result = new ArrayList<Integer>();
        for (MultiSelectNode node : selTreeRootNode.getRemovedChildren()) {
            result.add(new Integer(node.getId()));
        }
        return result;
    }
}
