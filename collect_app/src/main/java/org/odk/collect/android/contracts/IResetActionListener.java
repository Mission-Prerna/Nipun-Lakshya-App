package org.odk.collect.android.contracts;

import java.util.List;

public interface IResetActionListener {
    void onResetActionDone(List<Integer> failedResetActions);
}
