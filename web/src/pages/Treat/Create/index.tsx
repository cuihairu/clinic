
import React from 'react';
import { useRef } from 'react';
import {createTreat, fetchTreatById} from '@/services/ant-design-pro/treat';
import {fetchCustomerById} from "@/services/ant-design-pro/customer";
import {
  ProForm,
  ProFormSelect,
  ProFormText,
  ProFormDatePicker,
  ProFormTextArea,
  ProCard
} from '@ant-design/pro-components'
import type { ProFormInstance } from '@ant-design/pro-components';
import {message} from "antd";
import { useSearchParams,history } from '@umijs/max';

type Treat = {
  customerId?: number;
  // line 1
  name?: string;
  gender?: number;
  address?: string;
  birthday?: string;
  age?: number;
  firstTime?: string;
  desc?: string;
  inquiry?: string;
  observation?: string;
  palpation?: string;
  pulse?: string;
  pulseLeft?: string;
  pulseRight?: string;
  pulseInfoLeft?: string;
  pulseInfoRight?: string;
  fiveBenZiLeft?: string;
  fiveZiBenLeft?: string;
  fiveBenZiRight?: string;
  fiveZiBenRight?: string;
  fiveBenKeLeft?: string;
  fiveKeBenLeft?: string;
  fiveBenKeRight?: string;
  fiveKeBenRight?: string;
  fiveNanJinLeft?: string;
  fiveNanJinBigLeft?: string;
  fiveNanJinSmallLeft?: string;
  fiveNanJinRight?: string;
  fiveNanJinBigRight?: string;
  fiveNanJinSmallRight?: string;
  fiveRateLeft?: string;
  fiveRateRight?: string;
  fiveAuxLeft?: string;
  fiveAuxRight?:string;
  acupointLeft?:string;
  acupointRight?:string;
  diagnose?:string;
  plan?: string;
  diet?: string;
  conditioning?: string;
  review?: string;};

const Create: React.FC = () => {
  const [searchParams ] = useSearchParams();
  const formRef = useRef<ProFormInstance>();
  const customerId = searchParams.get("customerId");
  const treatId = searchParams.get("treatId");
  console.log(searchParams.get("customerId"));
  return (<ProForm<Treat>
    grid
    formRef={formRef}
    layout={'vertical'}
    request={async (d)=>{
      console.log(d)
      if (treatId){
       const ret = await fetchTreatById(treatId) || {};
        if (ret?.customerId){
          const v = await fetchCustomerById(ret?.customerId);
          ret.name = v?.name;
          ret.gender = v?.gender;
          ret.age = v?.age;
        }
        return Promise.resolve<Treat>(ret);
      }
      if (customerId && !isNaN(parseInt(customerId))){
        const v = await fetchCustomerById(customerId);
        return Promise.resolve<Treat>({
          customerId: parseInt(customerId),
          name: v?.name,
          gender:v?.gender,
          age:v?.age,
        });
      }
      message.error("没有发现顾客ID");
      return Promise.resolve<Treat>({});
    }}
    onFinish={async (values) => {
      const result = await createTreat(values);
      console.log(result)
      if (result.id) {
        formRef.current?.setFieldsValue({
          id:result.id
        })
        message.success("创建成功")
      }
      return true;
    }
    }
  >
    <ProFormText name={"id"} hidden={true}/>
    <ProFormText name={"customerId"} hidden={true}/>
    {/*第一行*/}
    <ProFormText colProps={{ md: 12, xl: 6 }} name="name" label="名字" placeholder="输入名字"/>
    <ProFormSelect colProps={{ md: 12, xl:3}} name="gender" label="性别" placeholder="男" valueEnum={{0:'女',1:'男'}} options={[{value:0,label:"女"},{value:1,label:"男"}]}/>
    <ProFormDatePicker colProps={{ md: 12, xl: 6 }} name="birthday" label="出生年月" placeholder="2000-01-01"/>
    <ProFormText colProps={{ md: 12, xl: 3 }} name="age" label="年龄" placeholder="20"/>
    <ProFormDatePicker colProps={{ md: 12, xl: 6 }} name="firstTime" label="初诊日期" placeholder="2000-01-01"/>
    {/*第二行*/}
    <ProFormTextArea colProps={{ md: 12, xl: 24 }} name="desc" label="主诉" placeholder="" />
    {/*第三行*/}
    <ProFormTextArea colProps={{ md: 12, xl: 24 }} name="inquiry" label="问诊" placeholder="" />
    {/*第四行*/}
    <ProFormTextArea colProps={{ md: 12, xl: 24 }} name="observation" label="望诊" placeholder="" />
    {/*第五行*/}
    <ProFormTextArea colProps={{ md: 12, xl: 24 }} name="palpation" label="触诊" placeholder="" />
    {/*第六行 脉象*/}
    <ProFormText colProps={{ md: 12, xl: 4 }} name="pulse" label="脉象" placeholder="脉象" />
    <ProFormText colProps={{ md: 12, xl: 10 }} name="pulseLeft" label="左手脉象" placeholder="" />
    <ProFormText colProps={{ md: 12, xl: 10 }} name="pulseRight" label="右手脉象" placeholder="" />
    {/*第七行*/}
    <ProFormTextArea colProps={{ md: 12, xl: 24 }} name="pulseInfo" label="脉象信息" placeholder="" />
    {/*第八行*/}

    <ProCard title={"五行脉用法"} ghost headerBordered split={"vertical"}>
      <ProCard title={"左手"} ghost headerBordered split={"horizontal"} layout="center">
        <ProCard title="子 _生_" ghost split={"vertical"}>
          <ProCard ghost>
            <ProFormText name="fiveBenZiLeft" label="本子" placeholder="" />
          </ProCard>
          <ProCard ghost>
            <ProFormText name="fiveZiBenLeft" label="子本" placeholder="" />
          </ProCard>
        </ProCard>
        <ProCard title="克 _克_" ghost split={"vertical"}>
          <ProCard ghost>
            <ProFormText name="fiveBenKeLeft" label="本子" placeholder="" />
          </ProCard>
          <ProCard ghost>
            <ProFormText name="fiveKeBenLeft" label="子本" placeholder="" />
          </ProCard>
        </ProCard>
        <ProCard ghost split={"vertical"}>
          <ProCard ghost>
            <ProFormText name="fiveNanJinLeft" label="难经" placeholder="" />
          </ProCard>
          <ProCard ghost>
            <ProFormText name="fiveNanJinBigLeft" label="大" placeholder="" />
          </ProCard>
          <ProCard ghost>
            <ProFormText name="fiveNanJinSmallLeft" label="小" placeholder="" />
          </ProCard>
        </ProCard>
        <ProCard ghost>
          <ProFormText name="fiveRateLeft" label="缓急" placeholder="" />
        </ProCard>
        <ProCard ghost>
          <ProFormText name="fiveAuxLeft" label="辅助" placeholder="" />
        </ProCard>
      </ProCard>
      <ProCard title={"右手"} ghost headerBordered split={"horizontal"} layout="center">
        <ProCard title="子 _生_" ghost split={"vertical"}>
          <ProCard ghost>
            <ProFormText name="fiveBenZiRight" label="本子" placeholder="" />
          </ProCard>
          <ProCard ghost>
            <ProFormText name="fiveZiBenRight" label="子本" placeholder="" />
          </ProCard>
        </ProCard>
        <ProCard title="克 _克_" ghost split={"vertical"}>
          <ProCard ghost>
            <ProFormText name="fiveBenKeRight" label="本子" placeholder="" />
          </ProCard>
          <ProCard ghost>
            <ProFormText name="fiveKeBenRight" label="子本" placeholder="" />
          </ProCard>
        </ProCard>
        <ProCard  ghost split={"vertical"}>
          <ProCard ghost>
            <ProFormText name="fiveNanJinRight" label="难经" placeholder="" />
          </ProCard>
          <ProCard ghost>
            <ProFormText name="fiveNanJinBigRight" label="大" placeholder="" />
          </ProCard>
          <ProCard ghost>
            <ProFormText name="fiveNanJinSmallRight" label="小" placeholder="" />
          </ProCard>
        </ProCard>
        <ProCard ghost>
          <ProFormText name="fiveRateRight" label="缓急" placeholder="" />
        </ProCard>
        <ProCard ghost>
          <ProFormText name="fiveAuxRight" label="辅助" placeholder="" />
        </ProCard>
      </ProCard>
    </ProCard>
    <ProCard title={"取穴"} bordered={false} ghost  layout="center" >
      <ProCard title="反射点法" bordered={false} split={"vertical"} ghost layout="center">
        <ProCard ghost>
          <ProFormText name="acupointLeft" label="取穴-反应点 左" placeholder="" />
        </ProCard>
        <ProCard ghost>
          <ProFormText name="acupointRight" label="取穴-反应点 右" placeholder="" />
        </ProCard>
      </ProCard>
    </ProCard>
    <ProCard  bordered={false} ghost layout="center">
      <ProFormTextArea name="diagnose" label="诊断" placeholder="" />
    </ProCard>
    <ProCard  bordered={false} ghost layout="center">
      <ProFormTextArea name="plan" label="方案" placeholder="" />
    </ProCard>
    <ProCard  bordered={false} ghost layout="center">
      <ProFormTextArea name="diet" label="饮食" placeholder="" />
    </ProCard>
    <ProCard  bordered={false} ghost layout="center">
      <ProFormTextArea name="conditioning" label="调理过程" placeholder="" />
    </ProCard>
    <ProCard bordered={false} ghost layout="center">
      <ProFormTextArea name="review" label="回访" placeholder="" />
    </ProCard>
  </ProForm>);
};

export default Create;
