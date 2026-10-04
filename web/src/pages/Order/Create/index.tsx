
import React from 'react';
import { createItem } from '@/services/ant-design-pro/item';
import {ProForm, ProFormText, ProFormTextArea, ProFormDatePicker, ProFormSelect} from '@ant-design/pro-components'
import {message} from "antd";

type Order = {
  id: number;
  customerId: number;
  customerName: string;
  itemId: number;
  staffId: number;
  extOrderId: string;
  payType: number;
  status: number;
  price: number;
  payment: number;
  createTime: string;
  updateTime: string;
}

const Create: React.FC = () => {
  return (
    <ProForm<Order>
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
      <ProFormText colProps={{ md: 4, xl: 4 }} name="customerName" label="顾客名字" tooltip="长度范围2-20字符,选填项" placeholder="" required={true} />
      <ProFormText colProps={{ md: 4, xl: 2 }} name="price" label="价格" tooltip="价格" placeholder="20"/>
    </ProForm>);
};

export default Create;
