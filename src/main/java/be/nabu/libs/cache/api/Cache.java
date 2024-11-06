/*
* Copyright (C) 2015 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.libs.cache.api;

import java.io.IOException;

public interface Cache {
	public boolean put(Object key, Object value) throws IOException;
	public Object get(Object key) throws IOException;
	public void clear(Object key) throws IOException;
	public void clear() throws IOException;
	public void prune() throws IOException;
	public void refresh() throws IOException;
	public void refresh(Object key) throws IOException;
}
