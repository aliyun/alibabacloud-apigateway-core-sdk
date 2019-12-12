﻿using System;

using AlibabaCloud.Apigateway.Utils;

using Xunit;

namespace baseClientTest.Utils
{
    public class TimeUtilsTest
    {
        [Fact]
        public void TestGetTimeMillis()
        {
            DateTime dt = Convert.ToDateTime("1970-01-01 00:00:00");
            Assert.Equal(0, dt.GetTimeMillis());
        }

        [Fact]
        public void TestGetGMTDate()
        {
            string GMTDate = TimeUtils.GetGMTDate();
            Assert.NotNull(GMTDate);
        }
    }
}
