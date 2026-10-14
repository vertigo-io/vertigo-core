/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2026, Vertigo.io, team@vertigo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.core.daemon;

/**
 * Snapshot of the daemon thread pool state.
 *
 * @param corePoolSize configured number of pool threads
 * @param activeThreads number of threads currently running a daemon
 * @param overdueTasks number of daemons whose next execution is due but waiting for a free thread (running daemons are not counted)
 * @param maxOverdueMillis how long the most overdue daemon has been waiting for a free thread, in millis (0 if none); for a daemon that never started, this is the only starvation signal
 * @author npiedeloup
 */
public record DaemonPoolStat(int corePoolSize, int activeThreads, int overdueTasks, long maxOverdueMillis) {
}
