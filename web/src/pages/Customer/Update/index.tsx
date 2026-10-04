
import React, {useRef} from 'react';
import {updateCustomer, fetchCustomerById} from '@/services/ant-design-pro/customer';
import {
  ProForm,
  ProFormText,
  ProFormTextArea,
  ProFormDatePicker,
  ProFormSelect,
  ProFormInstance
} from '@ant-design/pro-components'
import {message} from "antd";
import { useSearchParams,history } from '@umijs/max';
const Update: React.FC = () => {
  let [searchParams, ] = useSearchParams();
  const formRef = useRef<ProFormInstance>();
  let customerId = searchParams.get("customerId");
  if (customerId === null || customerId === undefined || customerId === "") {
    message.error("没有传入顾客id")
    history.push("/customer/query")
    return null;
  }
  const id =  parseInt(customerId);
  fetchCustomerById(customerId).then((value) => {
    console.log(value)
    formRef.current?.setFieldsValue({
      id: id,
      name: value.name,
      age: value.age,
      gender: value.gender,
      phone: value.phone,
      address: value.address,
      level: value.level,
      birthday: value.birthday,
    });
  });
  return (<ProForm<{
    id: number;
    name: string;
    age: number;
    gender: number;
    phone: string;
    address: string;
    level: number;
    birthday: string;
  }>
    formRef={formRef}
    grid
    layout={'vertical'}
    onFinish={async (values) => {
      values.id = id;
      const result = await updateCustomer(values);
      console.log(result)
      if (result.id ) {
        message.success("更新成功")
      }
      return true;
    }
    }
  >
    <ProFormText colProps={{ md: 12, xl: 8 }} name="name" label="姓名" tooltip="长度范围2-20字符,选填项" placeholder="输入员工名字" required={true}/>
    <ProFormText colProps={{ md: 12, xl: 4 }} name="age" label="年纪" tooltip="年纪" placeholder="20"/>
    <ProFormSelect colProps={{ md: 12, xl: 4 }} name="gender" label="性别" valueEnum={{0:'女',1:'男'}} options={[{value:0,label:"女"},{value:1,label:"男"}]} placeholder={'男'}/>
    <ProFormText colProps={{ md: 12, xl: 8 }} name="phone" label="手机号码" tooltip="方便按手机查找" placeholder="输入手机号码"/>
    <ProFormTextArea
      colProps={{ span: 24 }}
      name="address"
      label="详细的工作地址或家庭住址"
    />
    <ProFormText colProps={{ md: 12, xl: 2 }} name="level" label="会员等级" tooltip="会员等级，打折之类的使用" placeholder="1"/>
    <ProFormDatePicker
      colProps={{ xl: 8, md: 12 }}
      label="生日"
      name="birthday"
    />
  </ProForm>);
};

export default Update;
