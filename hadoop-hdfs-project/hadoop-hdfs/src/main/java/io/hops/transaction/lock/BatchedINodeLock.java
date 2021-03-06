/*
 * Copyright (C) 2015 hops.io.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hops.transaction.lock;

import io.hops.exception.StorageException;
import io.hops.exception.TransactionContextException;
import io.hops.metadata.hdfs.entity.INodeIdentifier;
import io.hops.transaction.EntityManager;
import org.apache.hadoop.hdfs.server.namenode.INode;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class BatchedINodeLock extends BaseINodeLock {

  private final List<INodeIdentifier> inodeIdentifiers;
  private int[] inodeIds;

  public BatchedINodeLock(List<INodeIdentifier> inodeIdentifiers) {
    this.inodeIdentifiers = inodeIdentifiers;
    inodeIds = new int[inodeIdentifiers.size()];
  }

  @Override
  protected void acquire(TransactionLocks locks) throws IOException {
    if (inodeIdentifiers != null && !inodeIdentifiers.isEmpty()) {
      String[] names = new String[inodeIdentifiers.size()];
      int[] parentIds = new int[inodeIdentifiers.size()];
      for (int i = 0; i < inodeIdentifiers.size(); i++) {
        INodeIdentifier inodeIdentifier = inodeIdentifiers.get(i);
        names[i] = inodeIdentifier.getName();
        parentIds[i] = inodeIdentifier.getPid();
        inodeIds[i] = inodeIdentifier.getInodeId();
      }

      find(DEFAULT_INODE_LOCK_TYPE, names, parentIds);
    } else {
      throw new StorageException(
          "INodeIdentifier object is not properly initialized ");
    }
  }

  private Collection<INode> find(TransactionLockTypes.INodeLockType lock,
      String[] names, int[] parentIds)
      throws StorageException, TransactionContextException {
    setINodeLockType(lock);
    Collection<INode> inodes = EntityManager
        .findList(INode.Finder.ByNamesAndParentIds, names, parentIds);
    for (INode inode : inodes) {
      addLockedINodes(inode, lock);
    }
    return inodes;
  }

  int[] getINodeIds() {
    return inodeIds;
  }
}
