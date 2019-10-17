// File generated by hadoop record compiler. Do not edit.
/**
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.zookeeper_voltpatches.proto;

import org.apache.jute_voltpatches.*;
import org.apache.zookeeper_voltpatches.data.ACL;
import org.apache.zookeeper_voltpatches.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GetACLResponse extends Record.AbstractRecord<GetACLResponse> {
    private List<ACL> acl;
    private Stat stat;
    public GetACLResponse() {
    }
    public GetACLResponse(List<ACL> acl, Stat stat) {
        this.acl = acl;
        this.stat = stat;
    }
    public List<ACL> getAcl() {
        return acl;
    }
    public void setAcl(List<ACL> m_) {
        acl = m_;
    }
    public Stat getStat() {
        return stat;
    }
    public void setStat(Stat m_) {
        stat = m_;
    }
    @Override
    public void serialize(OutputArchive a_, String tag) throws IOException {
        a_.startRecord(this,tag);
        a_.startVector(acl,"acl");
        if (acl!= null) {
            for (ACL e1 : acl) {
                a_.writeRecord(e1, "e1");
            }
        }
        a_.endVector(acl,"acl");
        a_.writeRecord(stat,"stat");
        a_.endRecord(this,tag);
    }
    @Override
    public void deserialize(InputArchive a_, String tag) throws IOException {
        a_.startRecord(tag);
        final Index vidx1 = a_.startVector("acl");
        if (vidx1 != null) {
            acl = new ArrayList<>();
            for (; !vidx1.done(); vidx1.incr()) {
                ACL e1;
                e1 = new ACL();
                a_.readRecord(e1,"e1");
                acl.add(e1);
            }
        }
        a_.endVector("acl");
        stat = new Stat();
        a_.readRecord(stat,"stat");
        a_.endRecord(tag);
    }

    @Override
    public int compareTo(GetACLResponse ignored) throws ClassCastException {
        throw new UnsupportedOperationException("comparing GetACLResponse is unimplemented");
    }
    @Override
    public boolean equals(Object peer_) {
        if (!(peer_ instanceof GetACLResponse)) {
            return false;
        } else {
            return peer_ == this || Comparator.comparing(GetACLResponse::getAcl, Utils::compareLists)
                    .thenComparing(GetACLResponse::getStat)
                    .compare(this, (GetACLResponse) peer_) == 0;
        }
    }
    @Override
    public int hashCode() {
        int result = 17;
        int ret = acl.hashCode();
        result = 37*result + ret;
        ret = stat.hashCode();
        return 37*result + ret;
    }
    public static String signature() {
        return "LGetACLResponse([LACL(iLId(ss))]LStat(lllliiiliil))";
    }
}
