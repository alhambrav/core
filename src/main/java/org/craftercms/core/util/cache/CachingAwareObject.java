/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.core.util.cache;

import java.util.Collection;
import java.util.List;

/**
 * Class description goes HERE
 *
 * @author Alfonso Vásquez
 */
public interface CachingAwareObject {

    String getScope();

    void setScope(String scope);

    Object getKey();

    void setKey(Object key);

    List<Object> getDependencyKeys();

    void setDependencyKeys(List<Object> dependencyKeys);

    void addDependencyKeys(Collection<Object> dependencyKeys);

    void addDependencyKey(Object dependencyKey);

    boolean removeDependencyKeys(Collection<Object> dependencyKeys);

    boolean removeDependencyKey(Object dependencyKey);

    Long getCachingTime();

    void setCachingTime(Long cachingTime);

}
