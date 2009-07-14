package edu.ualberta.med.biobank.treeview;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;

import edu.ualberta.med.biobank.forms.StudyEntryForm;
import edu.ualberta.med.biobank.forms.StudyViewForm;
import edu.ualberta.med.biobank.forms.input.FormInput;
import edu.ualberta.med.biobank.model.Study;

public class StudyAdapter extends Node {
	public static final int PATIENTS_NODE_ID = 0;

	private Study study;

	public StudyAdapter(Node parent, Study study) {
		super(parent);
		this.setStudy(study);

		if (study.getId() != null) {
			setId(study.getId());
			setName(study.getName());

		}
		addChild(new PatientGroup(this, PATIENTS_NODE_ID));
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	public Study getStudy() {
		return study;
	}

	@Override
	public Integer getId() {
		Assert.isNotNull(study, "study is null");
		return study.getId();
	}

	@Override
	public String getName() {
		Assert.isNotNull(study, "study is null");
		return study.getNameShort();
	}

	@Override
	public String getTitle() {
		return getTitle("Study");
	}

	@Override
	public void performDoubleClick() {
		openForm(new FormInput(this), StudyViewForm.ID);
	}

	@Override
	public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
		MenuItem mi = new MenuItem(menu, SWT.PUSH);
		mi.setText("Edit Study");
		mi.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				openForm(new FormInput(StudyAdapter.this), StudyEntryForm.ID);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		mi = new MenuItem(menu, SWT.PUSH);
		mi.setText("View Study");
		mi.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				openForm(new FormInput(StudyAdapter.this), StudyViewForm.ID);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	@Override
	public void loadChildren(boolean updateNode) {
	}

	@Override
	public Node accept(NodeSearchVisitor visitor) {
		return visitor.visit(this);
	}
}
