// src/pages/PasswordScreen.jsx
import { useState } from 'react';
import Layout from './Layout';
import InputField from './InputField';
import Button from './Button';

export default function PasswordScreen() {
  const [email, setEmail] = useState('');
  const handleReset = e => {
    e.preventDefault();
    alert(`${email}으로 재설정 링크를 보냈습니다.`);
  };

  return (
    <Layout>
      <form onSubmit={handleReset} className="bg-white rounded-2xl shadow-md p-8 w-full max-w-md mx-auto flex flex-col gap-6">
        <h2 className="text-2xl font-bold text-gray-900 mb-2">비밀번호 찾기</h2>
        <InputField label="이메일" type="email" placeholder="이메일 입력" value={email} onChange={e => setEmail(e.target.value)} required />
        <Button type="submit">비밀번호 재설정 링크 받기</Button>
      </form>
    </Layout>
  );
}