import { ProCard } from '@ant-design/pro-components';
import React, {useEffect, useState} from 'react';
import StaffReviewTable,{StaffReview} from "@/components/StaffReview";
import CustomerReviewTable,{CustomerReview} from "@/components/CustomerReview";
import ReviewTable,{Review} from "@/components/Review";
import {fetchReviewByDay, fetchReviewCustomerBulk, fetchReviewStaffBulk} from "@/services/ant-design-pro/review";
import moment from "moment";


const DayDashboard: React.FC = () => {
  const [staffReviews,setStaffReviews ] = useState<StaffReview[]>([])
  const [customerReviews,setCustomerReviews ] = useState<CustomerReview[]>([])
  const [reviews,setReviews ] = useState<Review[]>([])
  const now = new Date();
  useEffect( () => {
    fetchReviewStaffBulk(now).then((data)=>{
      setStaffReviews(data);
    })
    fetchReviewCustomerBulk(now).then((data)=>{
      setCustomerReviews(data);
    })
    fetchReviewByDay(now).then((data)=>{
      setReviews([data]);
    })
  },[]);
  return (
    <ProCard title={`今日报表:${moment().format("YYYY-MM-DD")}`} headerBordered split={"horizontal"}>
      <ProCard title={""}>
        <StaffReviewTable data={staffReviews} />
      </ProCard>
      <ProCard title={"今日回访"}>
        <CustomerReviewTable data={customerReviews}/>
      </ProCard>
      <ProCard title={"今日总结"}>
        <ReviewTable data={reviews} />
      </ProCard>
    </ProCard>
  );
};

export default DayDashboard;
