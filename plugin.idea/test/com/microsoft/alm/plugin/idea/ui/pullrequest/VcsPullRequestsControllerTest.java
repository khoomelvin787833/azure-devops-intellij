// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.ui.pullrequest;

import com.microsoft.alm.plugin.idea.IdeaAbstractTest;
import com.microsoft.alm.plugin.idea.ui.common.VcsTabStatus;
import com.microsoft.alm.plugin.idea.ui.common.tabs.TabImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.awt.event.ActionEvent;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VcsPullRequestsControllerTest extends IdeaAbstractTest {
    VcsPullRequestsController underTest;
    VcsPullRequestsModel modelMock;
    TabImpl uiMock;

    @Before
    public void setUp() {
        modelMock = Mockito.mock(VcsPullRequestsModel.class);
        uiMock = Mockito.mock(TabImpl.class);

        underTest = new VcsPullRequestsController();
        underTest.setModel(modelMock);
        underTest.setView(uiMock);
    }

    @Test
    public void testObservableActions() {
        //Add action on toolbar = create pull request
        underTest.update(null, VcsPullRequestsForm.CMD_CREATE_NEW_PULL_REQUEST);
        verify(modelMock).createNewItem();

        //Refresh
        underTest.update(null, VcsPullRequestsForm.CMD_REFRESH);
        verify(modelMock).loadData();

        //open selected pr
        underTest.update(null, VcsPullRequestsForm.CMD_OPEN_SELECTED_PR_IN_BROWSER);
        verify(modelMock).openSelectedItemsLink();
    }

    @Test
    public void testActionListener() {
        //click on status link

        //PR tab has completed loading
        when(modelMock.getTabStatus()).thenReturn(VcsTabStatus.LOADING_COMPLETED);
        underTest.actionPerformed(new ActionEvent(this, 0, VcsPullRequestsForm.CMD_STATUS_LINK));
        verify(modelMock).openGitRepoLink();

        //need credentials
        when(modelMock.getTabStatus()).thenReturn(VcsTabStatus.NO_AUTH_INFO);
        underTest.actionPerformed(new ActionEvent(this, 0, VcsPullRequestsForm.CMD_STATUS_LINK));
        verify(modelMock).loadData();

        //not a tf git repo
        when(modelMock.getTabStatus()).thenReturn(VcsTabStatus.NOT_TF_GIT_REPO);
        underTest.actionPerformed(new ActionEvent(this, 0, VcsPullRequestsForm.CMD_STATUS_LINK));
        verify(modelMock).importIntoTeamServicesGit();

        //pop up menu - open in browser
        underTest.actionPerformed(new ActionEvent(this, 0, VcsPullRequestsForm.CMD_OPEN_SELECTED_PR_IN_BROWSER));
        verify(modelMock).openSelectedItemsLink();

        //pop up menu - abandon pr
        underTest.actionPerformed(new ActionEvent(this, 0, VcsPullRequestsForm.CMD_ABANDON_SELECTED_PR));
        verify(modelMock).abandonSelectedPullRequest();
    }

    @Test
    public void testDefaultPropertyUpdates() {
        underTest.update(null, null);
        verify(uiMock).setStatus(any(VcsTabStatus.class));
        verify(modelMock).getTabStatus();
        verify(uiMock).setViewModel(any(PullRequestsTreeModel.class));
        verify(modelMock).getModelForView();
    }

    @Test
    public void testPropertyUpdates() {
        //Tab status updates
        underTest.update(null, VcsPullRequestsModel.PROP_PR_TAB_STATUS);
        verify(uiMock, times(1)).setStatus(any(VcsTabStatus.class));
        verify(modelMock, times(1)).getTabStatus();
    }
}
