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
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public final class FindMeetingQuery {

  private static final TimeRange START_OF_DAY_TIME_RANGE = TimeRange.fromStartDuration(TimeRange.START_OF_DAY, 0);

  public Collection<TimeRange> getOptimizedOpenTimes(List<TimeRange> conflictTimes, MeetingRequest request) {
    conflictTimes.add(START_OF_DAY_TIME_RANGE);
    Collections.sort(conflictTimes, TimeRange.ORDER_BY_START);
    final Collection<TimeRange> queryResult = new ArrayList<>();
    final int requestDuration = (int) request.getDuration();
    int maxEndingTimeSoFar = conflictTimes.get(0).end();
    for (int i = 1; i < conflictTimes.size(); i++) {
      final int durationDiff = conflictTimes.get(i).start() - maxEndingTimeSoFar; 
      if (durationDiff >= requestDuration) {
        queryResult.add(TimeRange.fromStartDuration(maxEndingTimeSoFar, durationDiff));
      }
      maxEndingTimeSoFar = Math.max(maxEndingTimeSoFar, conflictTimes.get(i).end());
    }
    if (TimeRange.END_OF_DAY - maxEndingTimeSoFar >= requestDuration) {
      queryResult.add(TimeRange.fromStartEnd(maxEndingTimeSoFar, TimeRange.END_OF_DAY, true));
    }
    return queryResult;
  }

  public Collection<TimeRange> queryOptimized(Collection<Event> events, MeetingRequest request) {
    final Map<String, List<TimeRange>> optAttToConflicts = new HashMap<>();
    List<TimeRange> mandConflictTimes = new ArrayList<>();
    for (final Event e : events) {
      for (final String eventAttendee : e.getAttendees()) {
        if (request.getAttendees().contains(eventAttendee)) {
          mandConflictTimes.add(e.getWhen());
        }
        if (request.getOptionalAttendees().contains(eventAttendee)) {
          getOptionalAttendees.putIfAbsent(eventAttendee, new ArrayList<TimeRange>()).add(e.getWhen());
        }
      }
    }
    final List<String> optAttendees = new ArrayList<>(request.getOptionalAttendees());
    Collection<TimeRange> queryOptResult =  new ArrayList<>();
    int maxOptAttendeesAttained = 0;
    for (final List<String> optAttSubset : powerSet(optAttendees)) {
      if (maxOptAttendeesAttained > optAttSubset.size()) {
        continue;
      }
      List<TimeRange> conflictTimes = new ArrayList<>(mandConflictTimes);
      for (final String optAttendee : optAttSubset) {
        conflictTimes.addAll(optAttToConflicts.getOrDefault(optAttendee, new ArrayList<>()));
      }
      final Collection<TimeRange> sol = getOptimizedOpenTimes(conflictTimes, request);
      if (!sol.isEmpty() && maxOptAttendeesAttained < optAttSubset.size()) {
        maxOptAttendeesAttained = optAttSubset.size();
        queryOptResult = sol;
      }
    }
    final Collection<TimeRange> mandatoryUserTimes = getOptimizedOpenTimes(mandConflictTimes, request);
    return (!mandatoryUserTimes.isEmpty() && !queryOptResult.isEmpty()) ? queryOptResult : mandatoryUserTimes; 
  }

  public List<List<String>> powerSet(List<String> optAttendees) {
    List<List<String>> result = new ArrayList<>();
    result.add(new ArrayList<String>());
    for (final String attendee : optAttendees) {
      for (int i = 0, n = result.size(); i < n; i++) {
        result.add(new ArrayList<String>(result[i]));
        result[i].add(optAttendees[i]);
      }
    }
    return result;
  }

}
