import React, { Fragment } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/master';
import { Icon } from 'choerodon-ui/pro';
import { handlePromptError } from '../../../../../utils';
import { useResourceStore } from '../../../stores';
import { useTreeStore } from './stores';

function AppItem({ name, record, intl: { formatMessage }, intlPrefix }) {
  const {
    treeDs,
    AppState: { currentMenuType: { id } },
  } = useResourceStore();
  const { treeItemStore } = useTreeStore();

  async function handleClick() {
    if (!record) return;

    const envId = record.get('parentId');
    const appId = record.get('id');
    try {
      const result = await treeItemStore.removeService(id, envId, [appId]);
      if (handlePromptError(result, false)) {
        treeDs.query();
      }
    } catch (error) {
      Choerodon.handleResponseError(error);
    }
  }

  function getSuffix() {
    const actionData = [{
      service: [],
      text: formatMessage({ id: `${intlPrefix}.modal.remove-service` }),
      action: handleClick,
    }];
    return <Action placement="bottomRight" data={actionData} />;
  }

  return <Fragment>
    <Icon type="widgets" />
    {name}
    {getSuffix()}
  </Fragment>;
}

AppItem.propTypes = {
  name: PropTypes.any,
};

export default injectIntl(observer(AppItem));
