import { useState } from 'react';

export function useApiStatus(initialStatus = 'idle') {
  const [status, setStatus] = useState(initialStatus);
  return { status, setStatus };
}

