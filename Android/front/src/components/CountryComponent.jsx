import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import BackendService from '../services/BackendService';

const CountryComponent = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [formData, setFormData] = useState({ name: '' });
    const [error, setError] = useState(null);

    // Загрузка данных страны при монтировании
    useEffect(() => {
        if (id !== '-1') {
            BackendService.retrieveCountry(id)
                .then(response => {
                    setFormData(response.data);
                })
                .catch(err => {
                    setError('Не удалось загрузить данные страны');
                    console.error(err);
                });
        }
    }, [id]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (id === '-1') {
                await BackendService.createCountry(formData);
            } else {
                await BackendService.updateCountry(formData);
            }
            navigate('/countries');
        } catch (err) {
            setError('Ошибка при сохранении: ' + (err.response?.data?.message || err.message));
            console.error(err);
        }
    };

    if (error) {
        return (
            <div className="alert alert-danger m-4">
                {error}
                <button onClick={() => navigate('/countries')} className="btn btn-link">
                    Вернуться к списку
                </button>
            </div>
        );
    }

    return (
        <div className="m-4">
            <h3>{id === '-1' ? 'Новая страна' : 'Редактирование страны'}</h3>
            <form onSubmit={handleSubmit}>
                <div className="mb-3">
                    <label className="form-label">Название страны</label>
                    <input
                        type="text"
                        className="form-control"
                        value={formData.name}
                        onChange={(e) => setFormData({...formData, name: e.target.value})}
                        required
                    />
                </div>
                <button type="submit" className="btn btn-primary">
                    Сохранить
                </button>
                <button
                    type="button"
                    className="btn btn-secondary ms-2"
                    onClick={() => navigate('/countries')}
                >
                    Отмена
                </button>
            </form>
        </div>
    );
};

export default CountryComponent;