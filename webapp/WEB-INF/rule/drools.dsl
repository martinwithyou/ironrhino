[when]total is less than {total}=o:Order(total<{total})
[then]taking the ship costs {amount}=o.setShipcost(new java.math.BigDecimal({amount}));