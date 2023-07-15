
import React from 'react';
import { createItem } from '@/services/ant-design-pro/item';
import {ProForm, ProFormText, ProFormTextArea } from '@ant-design/pro-components'
import {message} from "antd";

type Item = {
  id: number;
  name: string;
  price: number;
  description: string;
  createTime: string;
  updateTime: string;
}

const Update: React.FC = () => {
  return (
    <ProForm<Item>
      grid
      autoFocusFirstInput
      omitNil
      layout={'vertical'}
      onFinish={async (values) => {
        const result = await createItem(values);
        console.log(result)
        if (result.id) {
          message.success("创建成功")
          return true;
        }
        return false;
      }

      }
    >
      <ProFormText colProps={{ md: 4, xl: 4 }} name="name" label="卡项姓名" tooltip="长度范围2-20字符,选填项" placeholder="输入卡项名字" required={true} />
      <ProFormText colProps={{ md: 4, xl: 2 }} name="price" label="价格" tooltip="价格" placeholder="20"/>
      <ProFormTextArea colProps={{ md: 12, xl: 6 }} name="description" label="描述卡项的服务" tooltip="给顾客介绍卡项的内容" placeholder="养生专家"/>
    </ProForm>);
};

export default Update;
