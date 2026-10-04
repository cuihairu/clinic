import { EllipsisOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable, TableDropdown } from '@ant-design/pro-components';
import {Button, Dropdown, message} from 'antd';
import { useRef } from 'react';
import { queryItemByPage,deleteItem } from '@/services/ant-design-pro/item';
import { history } from '@umijs/max';


export const waitTimePromise = async (time: number = 100) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(true);
    }, time);
  });
};

export const waitTime = async (time: number = 100) => {
  await waitTimePromise(time);
};

type Item = {
  id?: number;
  name?: string;
  price?: number;
  description?: string;
  createTime?: string;
  updateTime?: string;
};

const columns: ProColumns<Item>[] = [
  {
    dataIndex: 'index',
    valueType: 'indexBorder',
    width: 48,
  },
  {
    dataIndex: 'id',
    hideInSearch: true,
    hideInTable: true,
  },
  {
    title: '名字',
    dataIndex: 'name',
    copyable: true,
    ellipsis: true,
    tip: '卡项名字',
    width: "12%",
    formItemProps: {
      rules: [
        {
          required: true,
          message: '此项为必填项',
        },
      ],
    },
  },
  {
    title: '简介',
    dataIndex: 'description',
    copyable: true,
    ellipsis: true,
    hideInSearch: true,
    width: "54%",
    tip: '介绍说明',
    formItemProps: {
      rules: [
        {
          required: true,
          message: '此项为必填项',
        },
      ],
    },
  },{
    title: '价格',
    dataIndex: 'price',
    ellipsis: true,
    width: "14%",
    formItemProps: {
      rules: [
        {
          required: true,
          message: '此项为必填项',
        },
      ],
    },
  },
  {
    title: '操作',
    valueType: 'option',
    key: 'option',
    render: (text, record, _, action) => [
      <a
        key="update"
        onClick={() => {
          if (record.id) {
            history.push(`/item/create?itemId=${record.id}`);
          }else{
            message.error('记录id不存,该数据可不可以编辑');
          }
        }}
      >
        更新
      </a>,
      <a
        key="delete"
        onClick={() => {
          if (record.id) {
            deleteItem(record.id).then(res => {
              if (res.id){
                message.success('删除成功');
                action?.reload();
              }else{
                message.warning('没有找到该卡项');
              }
            });
          }else{
            message.warning('记录id不存,无法删除,请刷新再试');
          }
        }}
      >
        删除
      </a>,
      <TableDropdown
        key="actionGroup"
        onSelect={() => action?.reload()}
        menus={[
        ]}
      />,
    ],
  },
];

export default () => {
  const actionRef = useRef<ActionType>();
  return (
    <ProTable<Item>
      columns={columns}
      actionRef={actionRef}
      cardBordered
      request={async (params = {}, sort, filter) => {
        console.log(sort, filter);
        const msg = await queryItemByPage(params);
        return {
          data: msg.data,
          success: true,
          total: msg.total,
        }
      }}
      editable={{
        type: 'multiple',
      }}
      columnsState={{
        persistenceKey: 'pro-table-singe-demos-Item',
        persistenceType: 'localStorage',
        onChange(value) {
          console.log('value: ', value);
        },
      }}
      rowKey="id"
      search={{
        labelWidth: 'auto',
      }}
      options={{
        setting: {
          listsHeight: 400,
        },
      }}
      form={{
        // 由于配置了 transform，提交的参与与定义的不同这里需要转化一下
        syncToUrl: (values, type) => {
          if (type === 'get') {
            return {
              ...values,
            };
          }
          return values;
        },
      }}
      pagination={{
        pageSize: 10,
        onChange: (page) => console.log(page),
      }}
      dateFormatter="string"
      headerTitle="已经查询到的卡项信息:"
      toolBarRender={() => [
        <Dropdown
          key="menu"
          menu={{
            items: [
              {
                label: '1st item',
                key: '1',
              },
            ],
          }}
        >
          <Button>
            <EllipsisOutlined />
          </Button>
        </Dropdown>,
      ]}
    />
  );
};
