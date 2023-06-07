/*
 * Copyright 2023 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.ejbclient.testsuite.integration.timers;

import jakarta.ejb.Remote;

import java.util.Date;
import java.util.Map;


/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@Remote
public interface StatisticsHoarder {

    TimerWrapper getTimerWrapper(String timerInfo);

    void addTimer(Object timerInfo, Date nextTimeout, long interval);

    void addTimeout(Object timerInfo, Date nextTimeout, long currentTime);

    void annihilateTimers();

    Map<String, TimerWrapper> getAllTimers();
}
