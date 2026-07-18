'use client';

import React, { useEffect, useState } from 'react';
import api from './api';

function OrderComponent({ orderId }) {
    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchOrder = async () => {
            try {
                const response = await api.get(`/orders/${orderId}`);
                setOrder(response.data);
            } catch (err) {
                if (err.response?.status === 401) {
                    window.location.href = '/oauth2/authorization/keycloak';
                } else {
                    setError(err.message);
                }
            } finally {
                setLoading(false);
            }
        };

        if (orderId) {
            fetchOrder();
        }
    }, [orderId]);

    if (loading) return <div>Loading...</div>;
    if (error) return <div>Error: {error}</div>;
    
    return (
        <div>
            <h2>Order Details</h2>
            <p>Order ID: {order?.orderId}</p>
            <p>Message: {order?.message}</p>
            <p>Requested By: {order?.requestedBy}</p>
        </div>
    );
}

export default OrderComponent;