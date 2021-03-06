/**
 * Copyright (c) Connexta
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ditto.replication.admin.query.replications.persist;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.NotFoundException;
import org.codice.ddf.admin.api.Field;
import org.codice.ddf.admin.api.fields.FunctionField;
import org.codice.ddf.admin.common.fields.base.BaseFunctionField;
import org.codice.ddf.admin.common.fields.base.scalar.BooleanField;
import org.codice.ddf.admin.common.fields.common.PidField;
import org.codice.ditto.replication.admin.query.ReplicationMessages;
import org.codice.ditto.replication.admin.query.ReplicationUtils;

public class SuspendReplication extends BaseFunctionField<BooleanField> {

  public static final String FIELD_NAME = "suspendReplication";

  public static final String DESCRIPTION =
      "Suspends/Enables a replication. Suspending/Enabling a replication already in the target state results in a no-op";

  public static final BooleanField RETURN_TYPE = new BooleanField();

  private PidField id;

  private BooleanField suspend;

  private ReplicationUtils replicationUtils;

  public SuspendReplication(ReplicationUtils replicationUtils) {
    super(FIELD_NAME, DESCRIPTION);
    this.replicationUtils = replicationUtils;
    id = new PidField("id");
    suspend = new BooleanField("suspend");
    id.isRequired(true);
    suspend.isRequired(true);
  }

  @Override
  public BooleanField performFunction() {
    BooleanField successful = new BooleanField();
    successful.setValue(replicationUtils.setConfigSuspended(id.getValue(), suspend.getValue()));

    return successful;
  }

  @Override
  public BooleanField getReturnType() {
    return RETURN_TYPE;
  }

  @Override
  public List<Field> getArguments() {
    return ImmutableList.of(id, suspend);
  }

  @Override
  public FunctionField<BooleanField> newInstance() {
    return new SuspendReplication(replicationUtils);
  }

  @Override
  public Set<String> getFunctionErrorCodes() {
    return ImmutableSet.of(ReplicationMessages.CONFIG_DOES_NOT_EXIST);
  }

  @Override
  public void validate() {
    super.validate();
    if (containsErrorMsgs()) {
      return;
    }
    try {
      replicationUtils.getConfigForId(id.getValue());
    } catch (NotFoundException e) {
      addErrorMessage(ReplicationMessages.configDoesNotExist());
    }
  }
}
