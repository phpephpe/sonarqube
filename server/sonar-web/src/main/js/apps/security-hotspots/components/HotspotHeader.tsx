/*
 * SonarQube
 * Copyright (C) 2009-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import React from 'react';
import { Link } from 'react-router';
import { translate } from '../../../helpers/l10n';
import { getRuleUrl } from '../../../helpers/urls';
import { Hotspot, HotspotStatusOption } from '../../../types/security-hotspots';
import Assignee from './assignee/Assignee';
import Status from './status/Status';

export interface HotspotHeaderProps {
  hotspot: Hotspot;
  onUpdateHotspot: (statusUpdate?: boolean, statusOption?: HotspotStatusOption) => Promise<void>;
}

export function HotspotHeader(props: HotspotHeaderProps) {
  const { hotspot } = props;
  const { message, rule } = hotspot;
  return (
    <div className="huge-spacer-bottom">
      <div className="display-flex-column big-spacer-bottom">
        <div className="big text-bold">{message}</div>
        <div className="spacer-top">
          <span className="note padded-right">{rule.name}</span>
          <Link className="small" to={getRuleUrl(rule.key)} target="_blank">
            {rule.key}
          </Link>
        </div>
      </div>
      <div className="display-flex-space-between">
        <Status
          hotspot={hotspot}
          onStatusChange={statusOption => props.onUpdateHotspot(true, statusOption)}
        />
        <div className="display-flex-end">
          <div className="display-inline-flex-center it__hs-assignee">
            <div className="big-spacer-right">{`${translate('assignee')}: `}</div>
            <Assignee hotspot={hotspot} onAssigneeChange={props.onUpdateHotspot} />
          </div>
        </div>
      </div>
    </div>
  );
}
