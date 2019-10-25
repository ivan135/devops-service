import React, { useEffect, Fragment, useState } from 'react';
import { observer } from 'mobx-react-lite';

import _ from 'lodash';

import MdEditor from '../../../../../components/MdEditor';

import './style/AppTagCreate.less';


export default observer((props) => {
  const { refresh, appTagStore, tagStore, modal, release: initRelease, projectId, appServiceId, tag } = props;
  const { formatMessage } = appTagStore;
  const [release, setRelease] = useState(initRelease);

  function handleNoteChange(value) {
    setRelease(value);
  }

  modal.handleOk(async () => {
    const res = await tagStore.editTag(projectId, tag, release, appServiceId);
    refresh();
  });

  return <Fragment>
    <div className="c7n-creation-title">{formatMessage({ id: 'apptag.release.title' })}</div>
    <MdEditor
      value={release}
      textMaxLength={1000}
      onChange={handleNoteChange}
    />
  </Fragment>;
});
