// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public final class FindMeetingQuery {

  private boolean eventHasConflict(Event e, MeetingRequest request) {
    final Set<String> eventAttendees = e.getAttendees();
    for (final String reqAttendee : request.getAttendees()) {
      if (eventAttendees.contains(reqAttendee)) {
        return true;
      }
    }
    return false;
  }

  private boolean eventHasConflictOpt(Event e, MeetingRequest request) {
    final Set<String> mandAttendees = request.getAttendees();
    final Set<String> optAttendees = request.getOptionalAttendees();
    for (final String eventAttendee : e.getAttendees()) {
      if (mandAttendees.contains(eventAttendee) || optAttendees.contains(eventAttendee)) {
        return true;
      }
    }
    return false;
  }

  public Collection<TimeRange> getOpenTimes(List<TimeRange> conflictTimes, MeetingRequest request) {
    Collections.sort(conflictTimes, TimeRange.ORDER_BY_START);
    final Collection<TimeRange> queryResult = new ArrayList<>();
    final int requestDuration = (int) request.getDuration();
    for (int i = 1; i < conflictTimes.size(); i++) {
      final int durationDiff = conflictTimes.get(i).start() - conflictTimes.get(i - 1).end(); 
      if (durationDiff >= requestDuration) {
        queryResult.add(TimeRange.fromStartDuration(conflictTimes.get(i - 1).end(), durationDiff));
      }
    }
    final int lastEventEndTime = conflictTimes.stream().map(e -> e.end()).max(Integer::compare).get();
    if (TimeRange.END_OF_DAY - lastEventEndTime >= requestDuration) {
      queryResult.add(TimeRange.fromStartEnd(lastEventEndTime, TimeRange.END_OF_DAY, true));
    }
    return queryResult;
  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimeRange> mandConflictTimes = new ArrayList<>();
    List<TimeRange> mandAndOptConflictTimes = new ArrayList<>();
    mandConflictTimes.add(TimeRange.fromStartDuration(TimeRange.START_OF_DAY, 0)); 
    for (final Event e : events) {
      if (eventHasConflict(e, request)) {
        mandConflictTimes.add(e.getWhen());
        mandAndOptConflictTimes.add(e.getWhen());
      } else if (eventHasConflictOpt(e, request)) {
        mandAndOptConflictTimes.add(e.getWhen());
      }
    }
    final Collection<TimeRange> mandatoryUserTimes = getOpenTimes(mandConflictTimes, request);
    final Collection<TimeRange> mandAndOptUserTimes = getOpenTimes(mandAndOptConflictTimes, request);
    return (mandAndOptUserTimes.isEmpty()) ? mandatoryUserTimes : mandAndOptUserTimes; 
  }
}
