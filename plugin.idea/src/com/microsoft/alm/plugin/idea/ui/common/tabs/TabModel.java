// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.ui.common.tabs;

import com.microsoft.alm.plugin.idea.ui.common.VcsTabStatus;
import com.microsoft.alm.plugin.operations.Operation;

import java.util.Observer;

/**
 * Interface for Tab models
 */
public interface TabModel<T> {
    String PROP_FILTER = "filter";

    VcsTabStatus getTabStatus();

    void setTabStatus(final VcsTabStatus status);

    T getModelForView();

    void loadData();

    void importIntoTeamServicesGit();

    void openSelectedItemsLink();

    void appendData(final Operation.Results results);

    void createNewItem();

    void setFilter(final String filter);

    String getFilter();

    void dispose();

    void addObserver(final Observer observer);
}
